package api

import (
	"context"
	"time"

	"github.com/krau/SaveAny-Bot/core"
	"github.com/krau/SaveAny-Bot/pkg/enums/tasktype"
)

// trackedTask wraps a core.Executable and updates the API progress
// store at the boundaries of Execute. Without this, tasks created via
// the HTTP API are pushed to the queue but never registered with the
// progress store, so GET /api/v1/tasks/ always returns an empty list
// even while the task is running.
type trackedTask struct {
	inner core.Executable
	info  *TaskProgressInfo
}

func (t *trackedTask) Type() tasktype.TaskType { return t.inner.Type() }
func (t *trackedTask) Title() string           { return t.inner.Title() }
func (t *trackedTask) TaskID() string          { return t.inner.TaskID() }

func (t *trackedTask) Execute(ctx context.Context) error {
	if t.info != nil {
		t.info.Status = TaskStatusRunning
		t.info.UpdatedAt = time.Now()
	}
	err := t.inner.Execute(ctx)
	if t.info == nil {
		return err
	}
	switch {
	case err == nil:
		t.info.Status = TaskStatusCompleted
	case ctx.Err() == context.Canceled:
		t.info.Status = TaskStatusCancelled
		t.info.Error = err.Error()
	default:
		t.info.Status = TaskStatusFailed
		t.info.Error = err.Error()
	}
	t.info.UpdatedAt = time.Now()
	return err
}

// trackAPITask registers a task with the progress store and returns a
// wrapper that keeps the store in sync with execution state.
func trackAPITask(taskID string, taskType tasktype.TaskType, storage, path string, inner core.Executable) core.Executable {
	info := RegisterTask(taskID, string(taskType), storage, path, inner.Title(), "")
	return &trackedTask{inner: inner, info: info}
}

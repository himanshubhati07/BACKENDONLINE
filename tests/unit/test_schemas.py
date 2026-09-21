# Unit tests for pydantic schema validation logic (no DB, no server)
import pytest
from pydantic import ValidationError

from app.schemas import TaskCreate, TaskUpdate


def test_task_create_defaults():
    task = TaskCreate(title="Do something")
    assert task.status.value == "TODO"
    assert task.priority.value == "MEDIUM"


def test_task_create_rejects_empty_title():
    with pytest.raises(ValidationError):
        TaskCreate(title="")


def test_task_create_rejects_title_too_long():
    with pytest.raises(ValidationError):
        TaskCreate(title="a" * 201)


def test_task_create_rejects_invalid_status():
    with pytest.raises(ValidationError):
        TaskCreate(title="ok", status="INVALID")


def test_task_create_rejects_invalid_priority():
    with pytest.raises(ValidationError):
        TaskCreate(title="ok", priority="URGENT")


def test_task_update_allows_partial_fields():
    update = TaskUpdate(status="COMPLETED")
    dumped = update.model_dump(exclude_unset=True)
    assert dumped == {"status": update.status}

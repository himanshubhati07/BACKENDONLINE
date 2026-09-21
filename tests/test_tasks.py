# Tests for Task CRUD endpoints, API key auth, validation, and not-found handling
import pytest

from tests.utils.factories import build_task_payload

pytestmark = pytest.mark.asyncio(loop_scope="session")


async def test_create_task(client, api_key_header):
    payload = build_task_payload(title="Buy groceries")
    response = await client.post("/api/v1/tasks", json=payload, headers=api_key_header)
    assert response.status_code == 201
    body = response.json()
    assert body["title"] == "Buy groceries"
    assert body["status"] == "TODO"
    assert body["priority"] == "MEDIUM"
    assert "id" in body


async def test_list_tasks(client, api_key_header):
    await client.post("/api/v1/tasks", json=build_task_payload(title="Task A"), headers=api_key_header)
    await client.post("/api/v1/tasks", json=build_task_payload(title="Task B"), headers=api_key_header)

    response = await client.get("/api/v1/tasks", headers=api_key_header)
    assert response.status_code == 200
    body = response.json()
    assert "items" in body
    assert "total" in body
    assert body["limit"] == 20
    assert body["offset"] == 0
    assert body["total"] >= 2


async def test_get_task(client, api_key_header):
    create_resp = await client.post(
        "/api/v1/tasks", json=build_task_payload(title="Get me"), headers=api_key_header
    )
    task_id = create_resp.json()["id"]

    response = await client.get(f"/api/v1/tasks/{task_id}", headers=api_key_header)
    assert response.status_code == 200
    assert response.json()["id"] == task_id


async def test_get_task_not_found(client, api_key_header):
    response = await client.get(
        "/api/v1/tasks/00000000-0000-0000-0000-000000000000", headers=api_key_header
    )
    assert response.status_code == 404


async def test_update_task(client, api_key_header):
    create_resp = await client.post(
        "/api/v1/tasks", json=build_task_payload(title="Update me"), headers=api_key_header
    )
    task_id = create_resp.json()["id"]

    response = await client.put(
        f"/api/v1/tasks/{task_id}",
        json={"status": "IN_PROGRESS", "priority": "HIGH"},
        headers=api_key_header,
    )
    assert response.status_code == 200
    body = response.json()
    assert body["status"] == "IN_PROGRESS"
    assert body["priority"] == "HIGH"


async def test_update_task_not_found(client, api_key_header):
    response = await client.put(
        "/api/v1/tasks/00000000-0000-0000-0000-000000000000",
        json={"status": "IN_PROGRESS"},
        headers=api_key_header,
    )
    assert response.status_code == 404


async def test_delete_task(client, api_key_header):
    create_resp = await client.post(
        "/api/v1/tasks", json=build_task_payload(title="Delete me"), headers=api_key_header
    )
    task_id = create_resp.json()["id"]

    response = await client.delete(f"/api/v1/tasks/{task_id}", headers=api_key_header)
    assert response.status_code == 200

    get_resp = await client.get(f"/api/v1/tasks/{task_id}", headers=api_key_header)
    assert get_resp.status_code == 404


async def test_delete_task_not_found(client, api_key_header):
    response = await client.delete(
        "/api/v1/tasks/00000000-0000-0000-0000-000000000000", headers=api_key_header
    )
    assert response.status_code == 404


async def test_create_task_invalid_data(client, api_key_header):
    response = await client.post(
        "/api/v1/tasks", json={"title": ""}, headers=api_key_header
    )
    assert response.status_code == 422

    response = await client.post(
        "/api/v1/tasks", json={"title": "ok", "status": "BOGUS"}, headers=api_key_header
    )
    assert response.status_code == 422


async def test_unauthorized_missing_key(client):
    response = await client.get("/api/v1/tasks")
    assert response.status_code == 401


async def test_unauthorized_invalid_key(client):
    response = await client.get("/api/v1/tasks", headers={"X-API-Key": "invalid-key"})
    assert response.status_code == 401

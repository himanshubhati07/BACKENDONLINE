# Tests for admin-only API key management endpoints
import pytest

pytestmark = pytest.mark.asyncio(loop_scope="session")


async def test_create_api_key(client, admin_headers):
    response = await client.post("/api/v1/api-keys/", json={"name": "my-key"}, headers=admin_headers)
    assert response.status_code == 201
    body = response.json()
    assert "api_key" in body
    assert "key_hash" not in body
    assert body["name"] == "my-key"


async def test_list_api_keys(client, admin_headers):
    await client.post("/api/v1/api-keys/", json={"name": "listed-key"}, headers=admin_headers)
    response = await client.get("/api/v1/api-keys/", headers=admin_headers)
    assert response.status_code == 200
    body = response.json()
    assert isinstance(body, list)
    for item in body:
        assert "key_hash" not in item
        assert "api_key" not in item


async def test_revoke_api_key(client, admin_headers):
    create_resp = await client.post("/api/v1/api-keys/", json={"name": "revoke-me"}, headers=admin_headers)
    key_id = create_resp.json()["id"]
    response = await client.delete(f"/api/v1/api-keys/{key_id}", headers=admin_headers)
    assert response.status_code == 200


async def test_admin_unauthorized(client):
    response = await client.post("/api/v1/api-keys/", json={"name": "no-admin"})
    assert response.status_code == 401

    response = await client.get("/api/v1/api-keys/")
    assert response.status_code == 401

    response = await client.delete("/api/v1/api-keys/00000000-0000-0000-0000-000000000000")
    assert response.status_code == 401

    bad_headers = {"X-Admin-Key": "wrong-key"}
    response = await client.get("/api/v1/api-keys/", headers=bad_headers)
    assert response.status_code == 401

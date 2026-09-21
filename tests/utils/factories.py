# Helper functions that build valid request payloads for tests
def build_task_payload(**overrides):
    payload = {
        "title": "Write documentation",
        "description": "Document the API endpoints",
        "status": "TODO",
        "priority": "MEDIUM",
    }
    payload.update(overrides)
    return payload


def build_api_key_payload(**overrides):
    payload = {"name": "test-key"}
    payload.update(overrides)
    return payload

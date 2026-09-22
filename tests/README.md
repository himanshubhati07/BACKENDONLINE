# Test Suite

Bash/curl based integration tests against a running instance of the API.

## Prerequisites
- The server must already be running (e.g. `bash start.sh` or the deployment script)
  and reachable at `BASE_URL` (defaults to `http://localhost:23516`).
- `ADMIN_API_KEY` must be set in the environment, or present in `.env_b053751c0f39d3e4`
  in the repo root (scripts source it automatically).
- `curl`, `python3` are required.

## Running

```bash
export BASE_URL=http://localhost:23516
bash tests/run_all.sh
```

Each `test_*.sh` script prints `PASSED` and exits 0 on success, or prints
`FAILED:<reason>` and exits 1 on failure.

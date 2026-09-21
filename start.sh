#!/usr/bin/env bash
set -e
export PYTHONUNBUFFERED=1
PORT="${PORT:-25518}"

if [ ! -d ".venv" ]; then
    python3 -m venv .venv
fi

# shellcheck disable=SC1091
source .venv/bin/activate

pip install --upgrade pip -q
pip install -r requirements.txt -q

exec uvicorn app.main:app --host 0.0.0.0 --port "$PORT" --reload

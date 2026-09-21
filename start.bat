@echo off
if not defined PORT set PORT=25518

if not exist ".venv" (
    python -m venv .venv
)

call .venv\Scripts\activate.bat

pip install --upgrade pip -q
pip install -r requirements.txt -q

uvicorn app.main:app --host 0.0.0.0 --port %PORT% --reload

python3 -m venv env 

source env/bin/activate      

uvicorn app.main:app --host 0.0.0.0 --reload
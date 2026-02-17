FROM python:3.14-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY payment_provider.py .

CMD ["uvicorn", "payment_provider:app", "--host", "0.0.0.0", "--port", "1080"]
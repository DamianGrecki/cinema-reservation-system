import os
import uuid
import hmac as hmac_lib
import hashlib
import base64
import json
import httpx
import asyncio
from fastapi import FastAPI, BackgroundTasks, Request

app = FastAPI()

CINEMA_SERVICE_URL = os.getenv("CINEMA_SERVICE_URL", "http://host.docker.internal:8080")
HMAC_SECRET = os.getenv("HMAC_SECRET", "sandbox-webhook-hmac-secret-min-32-chars")

@app.post("/api/payment")
async def create_payment(request: Request, background_tasks: BackgroundTasks):
    data = await request.json()
    transaction_id = str(uuid.uuid4())

    background_tasks.add_task(
        send_webhook,
        data.get("orderId"),
        transaction_id
    )

    return {
        "transactionId": transaction_id,
        "orderId": data.get("orderId"),
        "status": "PENDING",
    }

@app.post("/api/refund")
async def create_refund(request: Request):
    data = await request.json()
    refund_transaction_id = str(uuid.uuid4())

    return {
        "transactionId": refund_transaction_id,
        "orderId": data.get("orderId"),
        "status": "COMPLETED",
    }

def compute_signature(payload_str: str) -> str:
    key = HMAC_SECRET.encode("utf-8")
    msg = payload_str.encode("utf-8")
    digest = hmac_lib.new(key, msg, hashlib.sha256).digest()
    return base64.b64encode(digest).decode("utf-8")

async def send_webhook(order_id, transaction_id):
    # Payment processing delay
    await asyncio.sleep(10)

    webhook_url = f"{CINEMA_SERVICE_URL}/api/webhook/payment"
    payload = {
        "transactionId": transaction_id,
        "orderId": order_id,
        "status": "COMPLETED",
    }
    payload_str = json.dumps(payload)
    signature = compute_signature(payload_str)

    try:
        async with httpx.AsyncClient() as client:
            await client.post(
                webhook_url,
                content=payload_str,
                headers={
                    "Content-Type": "application/json",
                    "X-Signature": signature,
                },
            )
            print(f"Webhook sent for {transaction_id}")
    except Exception as e:
        print(f"Failed to send webhook: {e}")

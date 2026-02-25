import uuid
import httpx
import asyncio
from fastapi import FastAPI, BackgroundTasks, Request

app = FastAPI()

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

async def send_webhook(order_id, transaction_id):
    # Payment processing delay
    await asyncio.sleep(10)

    webhook_url = "http://host.docker.internal:8080/api/webhook/payment"
    payload = {
        "transactionId": transaction_id,
        "orderId": order_id,
        "status": "COMPLETED",
    }

    try:
        async with httpx.AsyncClient() as client:
            await client.post(webhook_url, json=payload)
            print(f"Webhook sent for {transaction_id}")
    except Exception as e:
        print(f"Failed to send webhook: {e}")
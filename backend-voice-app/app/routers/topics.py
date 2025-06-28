
from fastapi import APIRouter, Depends, HTTPException, status
from app.core.deps import get_current_user
from app.models.topic import TopicRead
from app.core.supabase import supabase_client
from typing import List

router = APIRouter()

@router.get("/topics", response_model=List[TopicRead])
async def read_topics(current_user: dict = Depends(get_current_user)):
    """
    Retrieve a list of all available topics.
    """
    try:
        res = supabase_client.table("topics").select("*").execute()
        return res.data
    except Exception:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="No se pudieron recuperar los temas en este momento."
        )

@router.post("/topics/{topic_id}/complete", status_code=status.HTTP_201_CREATED)
async def complete_topic(topic_id: int, current_user: dict = Depends(get_current_user)):
    """
    Mark a specific topic as completed for the current user.
    """
    try:
        user_id = current_user["user_id"]
        
        # 1. Verify the topic exists to provide a clear error message.
        topic_res = supabase_client.table("topics").select("id").eq("id", topic_id).single().execute()
        if not topic_res.data:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="El tema seleccionado no existe."
            )

        # 2. Insert the progress record.
        progress_data = {"user_id": user_id, "topic_id": topic_id}
        supabase_client.table("user_progress").insert(progress_data).execute()

        return {"message": "Tema marcado como completado exitosamente."}

    except Exception as e:
        # Handle unique constraint violation (user already completed the topic).
        if hasattr(e, 'code') and e.code == '23505': # unique_violation for PostgreSQL
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Ya has completado este tema."
            )
        # Handle other potential database or application errors.
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Ocurrió un error inesperado al guardar tu progreso."
        )

@router.get("/topics/completed", response_model=List[TopicRead])
async def get_completed_topics(current_user: dict = Depends(get_current_user)):
    """
    Get a list of topics completed by the current user.
    """
    try:
        user_id = current_user["user_id"]
        # Call the RPC function to get completed topics.
        res = supabase_client.rpc('get_completed_topics', {'p_user_id': user_id}).execute()
        return res.data if res.data else []
    except Exception:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="No se pudieron recuperar tus temas completados."
        )

from fastapi import APIRouter, Depends, HTTPException, status
from app.core.deps import get_current_user
from app.models.user import ProfileRead, ProfileUpdate
from app.core.supabase import supabase_client

router = APIRouter()

@router.get("/me", response_model=ProfileRead)
async def read_users_me(current_user: dict = Depends(get_current_user)):
    """
    Fetch the profile of the currently authenticated user.
    """
    try:
        user_id = current_user["user_id"]
        print(user_id)
        res = supabase_client.table("profiles").select("*").eq("id", user_id).single().execute()
        
        if not res.data:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="No se pudo encontrar tu perfil."
            )
        return res.data
    except Exception:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Ocurrió un error al obtener tu perfil."
        )

@router.patch("/me", response_model=ProfileRead)
async def update_user_me(profile_update: ProfileUpdate, current_user: dict = Depends(get_current_user)):
    """
    Update the profile of the currently authenticated user.
    """
    try:
        user_id = current_user["user_id"]
        # Use model_dump to handle Pydantic v2 changes
        update_data = profile_update.model_dump(exclude_unset=True)
        
        if not update_data:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Proporciona al menos un campo para actualizar."
            )

        res = supabase_client.table("profiles").update(update_data).eq("id", user_id).execute()
        
        if not res.data:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="No se encontró tu perfil para actualizar."
            )
        
        return res.data[0]
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Ocurrió un error al actualizar tu perfil."
        )

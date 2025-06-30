from fastapi import APIRouter, Depends, HTTPException, status
from app.core.deps import get_current_user
from app.core.supabase import get_supabase_client
from app.models.progress import ProgressLog, ProgressLogCreate
from postgrest import APIError
from typing import List

router = APIRouter()

@router.get("/progress", response_model=List[ProgressLog])
async def get_user_progress_history(
    current_user: dict = Depends(get_current_user),
    supabase = Depends(get_supabase_client)
):
    """
    Obtiene el historial de todos los registros de progreso para el usuario actual,
    ordenados del más reciente al más antiguo.
    """
    user_id = current_user["user_id"]
    if not user_id:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Authentication required")
        

    try:
        # Seleccionar todos los registros donde user_id coincida y ordenar por fecha descendente
        result = supabase.table("progress_logs") \
            .select("*") \
            .eq("user_id", user_id) \
            .order("session_date", desc=True) \
            .execute()

        return result.data

    except APIError as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error en la base de datos: {e.message}"
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Un error inesperado ocurrió: {str(e)}"
        )

@router.post("/progress", status_code=status.HTTP_201_CREATED)
async def log_user_progress(
    progress_data: ProgressLogCreate,
    current_user: dict = Depends(get_current_user),
    supabase = Depends(get_supabase_client)
):
    """
    Registra el progreso de una sesión de tutoría para el usuario actual.
    """
    # Crear el objeto ProgressLog completo con el user_id del usuario autenticado
    full_data = ProgressLog(
        user_id=current_user.get("user_id"),
        **progress_data.model_dump()
    )

    data_to_insert = full_data.model_dump(exclude_unset=True)
    data_to_insert['user_id'] = str(data_to_insert['user_id'])
    data_to_insert['session_date'] = data_to_insert['session_date'].isoformat().replace('+00:00', 'Z')

    print("user_id del token:", current_user.get("user_id"))
    print("user_id en el registro:", full_data.user_id)
    try:
        result = supabase.table("progress_logs").insert(data_to_insert).execute()
        if not result.data:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="No se pudo registrar el progreso. La base de datos no devolvió datos."
            )
        return result.data[0]
    except APIError as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error en la base de datos: {e.message}"
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Un error inesperado ocurrió: {str(e)}"
        )

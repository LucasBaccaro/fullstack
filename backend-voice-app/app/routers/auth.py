from fastapi import APIRouter, HTTPException, status, Depends
from app.models.user import UserCreate, UserLoginResponse, UserResponse, Token
from app.core.supabase import supabase_client
from gotrue.errors import AuthApiError
from app.core.deps import get_current_user
import httpx
from fastapi import Body

router = APIRouter()

@router.post("/signup", response_model=UserLoginResponse, status_code=status.HTTP_201_CREATED)
def sign_up(user_create: UserCreate):
    """
    Create a new user account without email verification.
    """
    try:
        # Step 1: Create the user in Supabase
        sign_up_res = supabase_client.auth.sign_up({
            "email": user_create.email,
            "password": user_create.password,
            "options": {
                'email_confirm': False
            }
        })

        # After a successful sign-up, sign in to get a session
        if sign_up_res.user:
            login_res = supabase_client.auth.sign_in_with_password({
                "email": user_create.email,
                "password": user_create.password,
            })

            if login_res.user is None or login_res.session is None:
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="No se pudo iniciar sesión después de registrar al usuario."
                )

            # Crear perfil en la tabla profiles si no existe
            user_id = login_res.user.id
            try:
                supabase_client.table("profiles").insert({
                    "id": user_id
                }).execute()
            except Exception as e:
                print("Error al crear perfil:", e)

            return UserLoginResponse(
                user=UserResponse(id=login_res.user.id, email=login_res.user.email),
                session=Token(access_token=login_res.session.access_token, token_type=login_res.session.token_type)
            )
        
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No se pudo crear el usuario en este momento."
        )

    except AuthApiError as e:
        # Friendly error messages based on Supabase responses
        if "User already registered" in e.message:
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Un usuario con este correo electrónico ya existe."
            )
        if "Password should be at least 6 characters" in e.message:
            raise HTTPException(
                status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
                detail="La contraseña debe tener al menos 6 caracteres."
            )
        if "Unable to validate email address" in e.message:
            raise HTTPException(
                status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
                detail="La dirección de correo electrónico no es válida."
            )
        # Generic fallback for other authentication errors
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Error de autenticación: {e.message}"
        )
    except Exception:
        # Catch-all for any other unexpected errors
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Ocurrió un error inesperado. Por favor, inténtalo de nuevo."
        )

@router.post("/login", response_model=UserLoginResponse)
def login(user_login: UserCreate):
    """
    Authenticate a user and return a session token.
    """
    try:
        res = supabase_client.auth.sign_in_with_password({
            "email": user_login.email,
            "password": user_login.password,
        })

        if res.user is None or res.session is None:
            # This case is often covered by AuthApiError, but it's a good safeguard.
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Correo electrónico o contraseña no válidos."
            )

        return UserLoginResponse(
            user=UserResponse(id=res.user.id, email=res.user.email),
            session=Token(access_token=res.session.access_token, token_type=res.session.token_type)
        )

    except AuthApiError:
        # It's a security best practice to not reveal whether the email exists or the password was wrong.
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Correo electrónico o contraseña no válidos."
        )
    except Exception:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Ocurrió un error inesperado. Por favor, inténtalo de nuevo."
        )

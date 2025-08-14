import { Injectable } from '@angular/core';

@Injectable()
export class SessionService
{
  /* Recupere l'id de l'user depuis le local storage */
  get userId(): number | null
  {
    const v = localStorage.getItem('userId');
    return v ? Number(v) : null;
  }

  /* Ajoute l'id de l'user dans le local storage */
  setUserId(id:number)
  {
      localStorage.setItem('userId', String(id));
  }
}

import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SessionService } from './session.service';

@Injectable()
export class AuthHeaderInterceptor implements HttpInterceptor
{
  constructor(private session: SessionService){}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>>
  {
    const userId = this.session.userId;
    if(userId)
    {
      req = req.clone({ setHeaders: { 'X-User-Id': String(userId) } });
    }

    return next.handle(req);
  }
}

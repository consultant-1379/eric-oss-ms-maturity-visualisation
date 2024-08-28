///
/// COPYRIGHT Ericsson 2021
///
///
///
/// The copyright to the computer program(s) herein is the property of
///
/// Ericsson Inc. The programs may be used and/or copied only with written
///
/// permission from Ericsson Inc. or in accordance with the terms and
///
/// conditions stipulated in the agreement/contract under which the
///
/// program(s) have been supplied.
///



import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { AuthService } from 'src/services/auth.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  private authService = inject(AuthService);

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    
    const login = req.url.includes('api/v1/login');
    
      const user = localStorage.getItem('user');
      const isExpired = user === null;
      const msg = user === null ? '' : 'Session expired. Please login again'

      if(!login){
        if(user === null || JSON.parse(user).expiresAt < Date.now()){
          this.authService.logout(isExpired, msg);
          return throwError(() => new Error(msg));
        } else {
          req = req.clone({
            setHeaders:{
                Authorization: `Bearer ${JSON.parse(user).token}`
            }
          })
        }
      }

    return next.handle(req);
  }
}

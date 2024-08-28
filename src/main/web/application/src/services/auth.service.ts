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



import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, tap } from 'rxjs';
import { User } from 'src/models/user';

export interface Session {
  isExpired : boolean;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService  {

  private http = inject(HttpClient);
  private router = inject(Router);

  private sessionExpired = new BehaviorSubject<Session>({isExpired: false, message: ''});
  public sessionExpiredVal = this.sessionExpired.asObservable();

  private userSub = new BehaviorSubject<User | null>(JSON.parse(localStorage.getItem('user')!));
  public user$ = this.userSub.asObservable();

  public get userValue(){
    return this.userSub.value;
  }

  login(username: string, password: string){
    return this.http.post<User>('/api/v1/login', {username : username.trim(), password : password.trim()})
          .pipe(
            tap(user => {
              localStorage.setItem('user', JSON.stringify(user));
              this.userSub.next(user);
              this.sessionExpired.next({isExpired: false, message: ''});
            })
          )
  }

  logout(isExpired = true, message: string){
    localStorage.clear();
    sessionStorage.clear();
    this.sessionExpired.next({isExpired, message});
    this.userSub.next(null);
    this.router.navigate(['/login']);
  }

}

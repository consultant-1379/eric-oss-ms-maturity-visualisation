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



import { AfterViewInit, Component, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, first, takeUntil, tap } from 'rxjs';
import { AuthService } from 'src/services/auth.service';

class FormStatus {
  loading = false;
  error = false;
  submitted = false;
  errorMessage ?: string
}

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements AfterViewInit, OnDestroy{

  user$ = this.authService.user$;
  private destroySubject = new Subject<void>();
  formStatus = new FormStatus();
  form : FormGroup;

  fullForm  = [
    { type: 'text', formControlName: 'username', placeholder: 'Username' },
    { type: 'password', formControlName: 'password', placeholder: 'Password' },
  ];

  constructor(private authService: AuthService, private formBuilder: FormBuilder, private route: ActivatedRoute, private router: Router){
    if(localStorage.getItem('user')){ 
      this.router.navigate(['/']);
    }

    this.authService.sessionExpiredVal
    .pipe(takeUntil(this.destroySubject))
    .subscribe(session => {
      this.formStatus.error = !session.isExpired;
      this.formStatus.errorMessage = session.message;
    });
    

    this.form = this.formBuilder.group({
      username : ['', Validators.required], 
      password : ['', Validators.required]
    });
  }

  ngAfterViewInit(): void {

    this.form.valueChanges.pipe(
        first(),
        takeUntil(this.destroySubject),
        tap(_ => {
          this.form.setErrors(null);
        })
      ).subscribe();
  }

  get f(){
    return this.form.controls;
  }

  onSubmit(){
    this.formStatus.submitted = true;
    if(this.f['username'].errors &&  this.f['password'].errors){
      this.formStatus.error = true;
      this.formStatus.errorMessage = 'Username and password required.'
      return;
    }

    this.formStatus.error = false;
    this.formStatus.loading = true;
    
    this.authService.login(this.f['username'].value, this.f['password'].value)
            .pipe(takeUntil(this.destroySubject))
            .subscribe({
              next: () => {
                const redirectUrl = this.route.snapshot.queryParams['redirectUrl'] || '/';
                this.router.navigateByUrl(redirectUrl);
              },
              error: error => {

                this.formStatus = {
                  loading: false, 
                  error: true, 
                  submitted: false, 
                  errorMessage: 'Invalid credentials.'
                };

                this.f['username'].setErrors(Validators.required)
                this.f['password'].setErrors(Validators.required)
              }
            })
  }

  ngOnDestroy(): void {
      this.destroySubject.next();
  }

}

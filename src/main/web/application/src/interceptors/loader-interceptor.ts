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



import  { Injectable } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { LoaderService } from '../services/loader.service';
import { Constants } from 'src/constants/constants';

@Injectable()
export class LoaderInterceptor implements HttpInterceptor {

    constructor(private loaderService: LoaderService){}

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const componentId = req.headers.get(Constants.HTTP_HEADER_NAME) || '';
        this.loaderService.requestStarted(componentId);
        return this.handler(next, req, componentId);
    }

    handler(next:any, req:any, componentId:string) {
        return next.handle(req)
            .pipe(
                tap(
                    (event)=> {
                        if(event instanceof HttpResponse) {
                            this.loaderService.requestEnded(componentId);
                        }
                    },
                    (error: HttpErrorResponse)=> {
                        this.loaderService.resetLoader(componentId);
                        throw error;
                    }

                )
            )
    }
}

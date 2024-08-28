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


import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ReportService } from '../services/report.service';


@Injectable()
export class ValidProductGuard implements CanActivate {

    constructor( private service: ReportService, private router: Router ) {
    }

    canActivate(
        next: ActivatedRouteSnapshot,
        state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
        const product = next.paramMap.get('product');
        return this.service.getProducts().pipe(
            map(products => {
                    if (products.productWiseMetadata.find(each => each.product === product) === undefined) {
                        this.router.navigate(['404']);
                        return false;
                    }
                    return true;
                }
            ));
    }
}

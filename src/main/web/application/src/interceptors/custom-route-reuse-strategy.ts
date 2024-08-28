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



import { Injectable } from "@angular/core";
import {
    ActivatedRouteSnapshot,
    DetachedRouteHandle,
    RouteReuseStrategy } from "@angular/router";

@Injectable()
export class CustomRouteReuseStrategy implements RouteReuseStrategy {
    shouldDetach(route: ActivatedRouteSnapshot): boolean { return false }
    store(route: ActivatedRouteSnapshot, handle: DetachedRouteHandle | null): void {}
    shouldAttach(route: ActivatedRouteSnapshot): boolean { return false }
    retrieve(route: ActivatedRouteSnapshot): DetachedRouteHandle | null { return null }
    shouldReuseRoute(future: ActivatedRouteSnapshot, curr: ActivatedRouteSnapshot): boolean { return false }
}
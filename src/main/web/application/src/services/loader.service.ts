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
import { BehaviorSubject, Observable, catchError, distinctUntilChanged, map, of, shareReplay } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LoaderService {
  private loaderStates: { [componentId: string]: number } = {};
  private loader$ = new BehaviorSubject<{ [componentId: string]: string }>({});

  constructor() { }

  getLoaderObserver(componentId: string): Observable<string> {
    return this.loader$.asObservable().pipe(
      shareReplay(1),
      map((states) => states[componentId] || 'start'),
      distinctUntilChanged(),
      catchError(error => {
        console.error('An error occurred:', error);
        return of('error');
      })
    );
  }

  requestStarted(componentId: string) {
    this.loaderStates[componentId] = (this.loaderStates[componentId] || 0) + 1;
    this.updateLoaderStates();
  }


  requestEnded(componentId: string) {
    this.loaderStates[componentId] = Math.max(0, (this.loaderStates[componentId] || 0) - 1);
    this.updateLoaderStates();
  }

  resetLoader(componentId: string) {
    if (componentId) {
      this.loaderStates[componentId] = 0;
      this.updateLoaderStates();
    }
  }

  private updateLoaderStates() {
    const updatedStates: { [componentId: string]: string } = {};
    for (const componentId in this.loaderStates) {
      updatedStates[componentId] = this.loaderStates[componentId] > 0 ? 'start' : 'stop';
    }
    this.loader$.next(updatedStates);
  }
}
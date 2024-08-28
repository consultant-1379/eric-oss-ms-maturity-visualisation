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


/*///
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
 ///*/

import {TestBed} from '@angular/core/testing';
import {AppComponent} from './app.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReportService } from 'src/services/report.service';
import { MatMenuModule } from '@angular/material/menu';

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        AppComponent
      ],
      imports: [ HttpClientTestingModule,MatMenuModule ],
    providers: [ReportService]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it(`should have as title 'report'`, () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('report');
  });

  /*it('should render title', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.content span')?.textContent).toContain('report app is running!');
  });*/
});

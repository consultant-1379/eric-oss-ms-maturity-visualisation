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



import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Observable, of } from 'rxjs';
import { Products } from 'src/models';
import { LoaderService } from 'src/services/loader.service';
import { ReportService } from 'src/services/report.service';
import { ProductDetailComponent } from '../product-detail/product-detail.component';
import { ProductNavigationComponent } from './product-navigation.component';

describe('ProductNavigationComponent', () => {
  let component: ProductNavigationComponent;
  let fixture: ComponentFixture<ProductNavigationComponent>;
  let mockReportService: Partial<ReportService>;
  let mockLoaderService: Partial<LoaderService>;
  let product: Products = {
    jobNotification  : [],
    JobsByEachProductMap:{"ADC":"4","AUTO APPS":"2"}
  }
  beforeEach(async(() => {
    mockReportService = {
      getProducts(): Observable<Products> { return of(product); }
    }
    TestBed.configureTestingModule({
      imports: [RouterTestingModule.withRoutes(
        [{path: '', component: ProductDetailComponent}, {path: 'product', component: ProductDetailComponent}])],
      declarations: [ProductNavigationComponent],
      providers: [
        {provide:ReportService, useValue: mockReportService},
        {provide:LoaderService, useValue: mockLoaderService}]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProductNavigationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

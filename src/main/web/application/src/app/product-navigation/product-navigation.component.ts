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

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, map, tap } from 'rxjs';
import { ProductWiseMetadata } from 'src/models';
import { ReportService } from '../../services/report.service';

@Component({
  selector: 'product-navigation',
  templateUrl: './product-navigation.component.html',
  styleUrls: ['./product-navigation.component.css']
})
export class ProductNavigationComponent implements OnInit {

  products$!: Observable<ProductWiseMetadata[]>;

  constructor(private reportService: ReportService, private router: Router) {
  }

  ngOnInit() {
    this.products$ = this.reportService.getProducts().pipe(
      map(products => products.productWiseMetadata),
      tap(products => {
        const defaultRoute = products[0].product;
        this.router.navigate(['/products', sessionStorage.getItem("currentRoute") ||  defaultRoute ] )
      } )
    )
  }
}

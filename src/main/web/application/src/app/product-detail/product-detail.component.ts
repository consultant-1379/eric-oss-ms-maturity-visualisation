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

import { Component } from '@angular/core';

import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { Observable, Subscription, map } from 'rxjs';
import { ProductDetail, productsInfo } from 'src/constants/products-info';
import { MetaData, Report } from '../../models';
import { ReportService } from '../../services/report.service';


@Component({
    selector: 'product-detail',
    templateUrl: './product-detail.component.html',
    styleUrls: ['./product-detail.component.css']
})
export class ProductDetailComponent {

    customJobs: string[] = [];
    filterData: Report[] = [];

    product!: string;
    techTypes$!: Observable<MetaData[]>;
    private _routeListener: Subscription;
    productDetail!: ProductDetail;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private reportService: ReportService) {
        this._routeListener = this.router.events.subscribe((val) => {
            if (val instanceof NavigationEnd) {
                this.setProductFromParams();
            }
        });
    }

    private setProductFromParams(): void {
        this.product = this.route.snapshot.paramMap.get('product') || '';
        sessionStorage.setItem("currentRoute", this.product);
        this.productDetail = { title: productsInfo[this.product]?.title, description: productsInfo[this.product]?.description };
        this.techTypes$ = this.reportService.getProducts().pipe(
            map(products => products.productWiseMetadata.find( p => p.product === this.product )?.metaData ?? [])
        );
    }

    ngOnDestroy() {
        this._routeListener.unsubscribe();
    }
}

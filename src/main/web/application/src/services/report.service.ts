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


import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table/table-data-source';
import { Observable, shareReplay } from 'rxjs';
import { Cell, Column, Products, Reports } from '../models';
import { Constants } from 'src/constants/constants';

@Injectable({
  providedIn: 'root'
})
export class ReportService {

  private productData!:Observable<Products>;
  private tableData: {[key: string]: { columns: Column[] , dataSource: MatTableDataSource<Cell[]> }} = {};

  constructor(
    private http: HttpClient) {
      this.productData = this.fetchProducts();
}

  public setData(key: string, value: any) {
      this.tableData[key] = value;
    }

  public getData(key: string) {
      return this.tableData[key];
    }

  public getProductReports(product: string, app_type: string, type: string, page:number, size:number, headers: HttpHeaders): Observable<Reports> {
    return this.http.get<Reports>(`/api/v1/products/${product}/report/${app_type}/${type}?pageNo=${page}&pageSize=${size}`, { headers }).pipe(shareReplay(1))
  }

  public fetchProducts(): Observable<Products> {
    const headers = new HttpHeaders().set(Constants.HTTP_HEADER_NAME, Constants.PRODUCTS);
    return this.http.get<Products>(`/api/v1/products`, {headers}).pipe(shareReplay(1));
  }

  public getProducts(): Observable<Products> {
    return this.productData;
  }

}

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


import { CdkTableModule } from '@angular/cdk/table';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialogModule } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouteReuseStrategy } from '@angular/router';
import { JwtInterceptor } from 'src/interceptors/jwt.interceptor';
import { CustomRouteReuseStrategy } from '../interceptors/custom-route-reuse-strategy';
import { LoaderInterceptor } from '../interceptors/loader-interceptor';
import { ModelsModule } from '../models/models.module';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CellDialogComponent } from './dashboard/cell/cell-dialog/cell-dialog.component';
import { CellEmptyComponent } from './dashboard/cell/cell-empty/cell-empty.component';
import { CellHeaderComponent } from './dashboard/cell/cell-header/cell-header.component';
import { CellStageComponent } from './dashboard/cell/cell-stage/cell-stage.component';
import { CellComponent } from './dashboard/cell/cell.component';
import { ColumnHeaderComponent } from './dashboard/column-header/column-header.component';
import {
    DashboardFilterHelpDialogComponent
} from './dashboard/dashboard-filter-help-dialog/dashboard-filter-help-dialog.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { LoaderComponent } from './loader/loader.component';
import { LoginComponent } from './login/login.component';
import { ProductDetailComponent } from './product-detail/product-detail.component';
import { ProductNavigationComponent } from './product-navigation/product-navigation.component';
import { CellCustomStageComponent } from './dashboard/cell/cell-custom-stage/cell-custom-stage.component';

@NgModule({
    declarations: [
        AppComponent,
        DashboardComponent,
        DashboardFilterHelpDialogComponent,
        ColumnHeaderComponent,
        CellComponent,
        CellDialogComponent,
        CellEmptyComponent,
        CellHeaderComponent,
        CellStageComponent,
        ProductNavigationComponent,
        ProductDetailComponent,
        LoaderComponent,
        LoginComponent,
        CellCustomStageComponent
    ],
    imports: [
        AppRoutingModule,
        BrowserAnimationsModule,
        BrowserModule,
        ReactiveFormsModule,
        CdkTableModule,
        FlexLayoutModule,
        FormsModule,
        HttpClientModule,
        MatButtonModule,
        MatCardModule,
        MatDialogModule,
        MatIconModule,
        MatInputModule,
        MatTableModule,
        MatToolbarModule,
        MatTooltipModule,
        ModelsModule,
        MatExpansionModule,
        MatSelectModule,
        MatMenuModule,
        MatProgressSpinnerModule
    ],
    providers: [
        {provide: HTTP_INTERCEPTORS, useClass:JwtInterceptor, multi:true},
        {provide: HTTP_INTERCEPTORS, useClass:LoaderInterceptor, multi:true},
        {provide: RouteReuseStrategy, useClass: CustomRouteReuseStrategy}
    ],
    bootstrap: [AppComponent],
})
export class AppModule {
}

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


import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from 'src/guards/auth.guard';
import { ValidProductGuard } from '../guards/valid-product.guard';
import { LoginComponent } from './login/login.component';
import { ProductDetailComponent } from './product-detail/product-detail.component';

const routes: Routes = [
    { path: 'login', component: LoginComponent },
    {
        path: 'products',
        children: [
            {
                path: '',
                component: ProductDetailComponent,
                canActivate: [ValidProductGuard]
            },
            {
                path: ':product',
                component: ProductDetailComponent,
                canActivate: [AuthGuard, ValidProductGuard],
            },
        ],
    },
    { path: '', redirectTo: 'products', pathMatch: 'full' },
    {path: '**', redirectTo: ''},
];

@NgModule({
    imports: [RouterModule.forRoot(routes, { useHash: true })],
    exports: [RouterModule],
    providers: [ValidProductGuard]
})
export class AppRoutingModule {
}

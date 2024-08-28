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
import { Observable } from 'rxjs';
import { User } from 'src/models/user';
import { AuthService } from 'src/services/auth.service';
import { LoaderService } from 'src/services/loader.service';
import { ReportService } from 'src/services/report.service';
import { Constants } from '../constants/constants';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'report';
  cicdText: string = Constants.CICD_REPORTDSB;
  feedback: string = Constants.FEEDBACK;
  feedbackUrl: any = Constants.FEEDBACK_URL;
  feedbackTemplate: any = Constants.FEEDBACK_TEMPLATE;
  support: string = Constants.SUPPORT;
  supportUrl: any = Constants.SUPPORT_URL;
  supportTemplate: any = Constants.SUPPORT_TEMPLATE;
  mmtFeedback: string = Constants.MMT_FEEDBACK;
  userGuide: string = Constants.USER_GUIDE;
  userGuideURL: string = Constants.USER_GUIDE_URL;
  notificationList: string[] = [];
  emptyArrMsg: string[] = ['No new jobs added'];
  loader$ = this.loaderService.getLoaderObserver(Constants.PRODUCTS);
  user$ : Observable<User | null> ;

  constructor(private authService: AuthService, private reportService: ReportService, private loaderService: LoaderService) {
    this.user$ = authService.user$;
  }

  logout(){
    this.authService.logout(false, 'Logged Out Successfully');
  }

  getNotificFunction() {
    this.reportService.getProducts()
        .subscribe(data => {
          if(data?.jobNotification.length === 0) {
            this.notificationList = this.emptyArrMsg;
          } else {
            this.notificationList = data?.jobNotification;
          }
    });
  }
}

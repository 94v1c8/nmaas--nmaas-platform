import {Component, OnInit, ViewEncapsulation, Input, ViewChild} from '@angular/core';
import {Application} from '../../../../model/application';
import {AppImagesService} from '../../../../service/appimages.service';
import {RateComponent} from '../../../rate/rate.component';
import {DefaultLogo} from '../../../../directive/defaultlogo.directive';

import {isUndefined} from 'util';
import {SecurePipe} from '../../../../pipe/index';
import {Router} from "@angular/router";
import {AppInstallModalComponent} from "../../../modal/appinstall";
import {AppConfigService} from "../../../../service";
import {AuthService} from "../../../../auth/auth.service";
import {TranslateService} from "@ngx-translate/core";
import {AppDescription} from "../../../../model/appdescription";

@Component({
  selector: 'nmaas-applist-element',
  providers: [DefaultLogo, RateComponent, AppImagesService, SecurePipe, AppInstallModalComponent],
  templateUrl: './appelement.component.html',
  styleUrls: ['./appelement.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class AppElementComponent implements OnInit {

  @Input()
  public app: Application;

  @Input()
  public selected: boolean;

  @Input()
  public domainId: number;

  @ViewChild(AppInstallModalComponent)
  public readonly modal:AppInstallModalComponent;

  constructor(public appImagesService: AppImagesService, public AppConfigService: AppConfigService, public router:Router,
              public authService:AuthService, public translate:TranslateService) {
  }

  ngOnInit() {
    if (isUndefined(this.selected)) {
      this.selected = false;
    }
  }

  public showDeployButton():boolean {
    return this.domainId !== this.AppConfigService.getNmaasGlobalDomainId() && !this.authService.hasDomainRole(this.domainId, 'ROLE_GUEST');
  }

  public getDescription(): AppDescription {
    return this.app.descriptions.find(val => val.language == this.translate.currentLang);
  }
}

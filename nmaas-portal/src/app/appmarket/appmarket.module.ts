import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

import {AuthModule} from '../auth/auth.module';

import {AppMarketComponent} from './appmarket.component';
import {AppListModule} from './applist/applist.module';
import {AppDetailsComponent} from './appdetails/index';
import {AppInstanceModule} from './appinstance/appinstance.module';

import {NavbarComponent} from './navbar/index';

import {SharedModule} from '../shared/shared.module';

import {AppsService} from '../service/apps.service';
import {DomainService} from '../service/domain.service';
import {TagService} from '../service/tag.service';
import {UserService} from '../service/user.service';

import {AppInstallModalComponent} from './modals/appinstall/appinstallmodal.component';

import {PipesModule} from '../pipe/pipes.module';
import {DomainsModule} from './domains/domains.module';
import {UsersModule} from './users/users.module';

@NgModule({
  declarations: [
    AppMarketComponent,
    AppDetailsComponent,
    NavbarComponent,
    AppInstallModalComponent
  ],
  imports: [
    FormsModule,
    CommonModule,
    RouterModule,
    SharedModule,
    AppListModule,
    AppInstanceModule,
    DomainsModule,
    UsersModule,
    AuthModule,
    PipesModule
  ],
  exports: [
    AppMarketComponent,
      NavbarComponent
  ],
  providers: [
    AppsService,
    DomainService,
    UserService,
    TagService,
    UserService
  ]

})
export class AppMarketModule {}

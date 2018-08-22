import {RegistrationService} from '../auth/registration.service';
import {LoginComponent} from './login';
import {LogoutComponent} from './logout/logout.component';
import {ChangelogComponent} from './changelog/changelog.component';
import {PipesModule} from '../pipe/pipes.module';
import {SharedModule} from '../shared/shared.module';
import {RegistrationComponent} from './registration/registration.component';
import {ChangelogService} from '../service/changelog.service';
import {WelcomeComponent} from './welcome.component';
import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {ProfileComponent} from './profile/profile.component';
import {AppMarketModule} from "../appmarket";
import {UserService} from "../service";
import {CompleteComponent} from "./complete/complete.component";
import {TermsOfUseComponent} from './terms-of-use/terms-of-use.component';
import {ContentDisplayService} from "../service/content-display.service";
import { NmaasModalTermsComponent } from './nmaas-modal-terms/nmaas-modal-terms.component';

@NgModule({
  declarations: [
    WelcomeComponent,
    LoginComponent,
    LogoutComponent,
    ChangelogComponent,
    RegistrationComponent,
    ProfileComponent,
    CompleteComponent,
    TermsOfUseComponent,
    NmaasModalTermsComponent,
  ],
  imports: [
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    RouterModule,
    SharedModule,
    PipesModule,
      AppMarketModule
  ],
  exports: [
    WelcomeComponent
  ],
  providers: [
    RegistrationService,
    UserService,
    ChangelogService,
      ContentDisplayService
  ]
})
export class WelcomeModule {}
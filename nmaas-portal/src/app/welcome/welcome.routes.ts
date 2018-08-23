import {LoginComponent} from './login/login.component';
import {LogoutComponent} from './logout/logout.component';
import {RegistrationComponent} from './registration/registration.component';
import {ChangelogComponent} from './changelog/changelog.component';
import {Routes} from '@angular/router';
import {WelcomeComponent} from './welcome.component';
import {ProfileComponent} from "./profile/profile.component";
import {AuthGuard} from "../auth/auth.guard";
import {ComponentMode} from "../shared";
import {CompleteComponent} from "./complete/complete.component";
import {TermsOfUseComponent} from "./terms-of-use/terms-of-use.component";

export const WelcomeRoutes: Routes = [
    {
      path: 'welcome',
      component: WelcomeComponent,
      children: [
        { path: '', redirectTo: 'login', pathMatch: 'full'  },
        { path: 'login', component: LoginComponent},
        { path: 'registration', component: RegistrationComponent }
      ]
    },
    //{ path: 'terms-of-use', component: TermsOfUseComponent },
    { path: 'logout', component: LogoutComponent },
    { path: 'changelog', component: ChangelogComponent },
    { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard], data: {mode: ComponentMode.PROFILVIEW} },
    { path: 'complete', component: CompleteComponent, canActivate: [AuthGuard] }
];

import {RegistrationService} from '../../auth/registration.service';
import {Domain} from '../../model/domain';
import {Registration} from '../../model/registration';
import {AppConfigService} from '../../service/appconfig.service';
import {PasswordValidator} from '../../shared/common/password/password.component';
import {Component, OnInit, ViewChild} from '@angular/core';
import {FormGroup, FormBuilder, Validators} from '@angular/forms';
import {Observable} from 'rxjs/Observable';
import {ModalInfoTermsComponent} from "../../shared/modal/modal-info-terms/modal-info-terms.component";
import {ModalInfoPolicyComponent} from "../../shared/modal/modal-info-policy/modal-info-policy.component";
import {ModalComponent} from "../../shared/modal";

import {ReCaptchaComponent, ReCaptchaModule} from "angular5-recaptcha";
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'nmaas-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.css'],
    providers: [ModalComponent, ModalInfoTermsComponent, ModalInfoPolicyComponent]
})
export class RegistrationComponent implements OnInit {


  public sending: boolean = false;
  public submitted: boolean = false;
  public success: boolean = false;
  public errorMessage: string = '';

  @ViewChild(ReCaptchaComponent)
  captcha: ReCaptchaComponent;

  @ViewChild(ModalComponent)
  public readonly  modal: ModalComponent;

  @ViewChild(ModalInfoTermsComponent)
  public readonly modalInfoTerms: ModalInfoTermsComponent;

  @ViewChild(ModalInfoPolicyComponent)
  public readonly modalInfoPolicy: ModalInfoPolicyComponent;

  public registrationForm: FormGroup;
  public domains: Observable<Domain[]>;

  private loginFailureErrorMessage = '';
  private language = '';

  constructor(private fb: FormBuilder,
              private registrationService: RegistrationService,
              private appConfig: AppConfigService,
              private translate: TranslateService) {
    this.language = this.translate.getBrowserLang();
    this.registrationForm = fb.group(
      {
        username: ['', [Validators.required, Validators.minLength(3)]],
        newPassword: ['', Validators.required],
        confirmPassword: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        firstname: [''],
        lastname: [''],
        domainId: [null],
          termsOfUseAccepted: [false],
          privacyPolicyAccepted: [false],
      },
      {
        validator: PasswordValidator.match
      });
    this.translate.get(['LOGIN.LOGIN_FAILURE_MESSAGE'])
      .subscribe((response: string) => {
        this.loginFailureErrorMessage = Object.values(response)[0];
        // this.loginFailureErrorMessage = response;
      });
  }

  ngOnInit() {
    this.domains = this.registrationService.getDomains()
      .map((domains) => domains.filter((domain) => domain.id !== this.appConfig.getNmaasGlobalDomainId()));
  }

  public onSubmit(): void {
      let token = this.captcha.getResponse();
      if(token.length < 1){
          this.sending = false;
          this.submitted = true;
          this.success = false;
          this.errorMessage = "You have to prove that you're not a robot!";
      }
      else {
          if (!this.registrationForm.controls['termsOfUseAccepted'].value || !this.registrationForm.controls['privacyPolicyAccepted'].value) {
              this.sending = false;
              this.submitted = true;
              this.success = false;
              this.errorMessage = "You have to accept Terms of Use and Privacy Policy!"
          } else {
              if (this.registrationForm.valid) {
                  this.sending = true;

                  const registration: Registration = new Registration(
                      this.registrationForm.controls['username'].value,
                      this.registrationForm.controls['newPassword'].value,
                      this.registrationForm.controls['email'].value,
                      this.registrationForm.controls['firstname'].value,
                      this.registrationForm.controls['lastname'].value,
                      this.registrationForm.controls['domainId'].value,
                      this.registrationForm.controls['termsOfUseAccepted'].value,
                      this.registrationForm.controls['privacyPolicyAccepted'].value,
                      this.language
                  );
                  this.registrationService.register(registration).subscribe(
                      (result) => {
                          console.log("User registred successfully.");
                          this.registrationForm.reset();
                          this.sending = false;
                          this.submitted = true;
                          this.success = true;
                          this.modal.show();
                      },
                      (err) => {
                          console.log("Unable to register user.");
                          this.sending = false;
                          this.submitted = true;
                          this.success = false;
                          this.errorMessage = this.getMessage(err);
                      },
                      () => {
                          this.sending = false;
                          this.submitted = true;
                          console.log("Hmmm...");
                      }
                  );

              }
          }
      }
  }

  public refresh(): void {
    this.sending = false;
    this.submitted = false;
    this.success = false;
    this.errorMessage = '';    
  }

  public getMessage(err:string){
      return err.match("409")?"Invalid input data":"Service is unavailable. Please try again later";
  }

}

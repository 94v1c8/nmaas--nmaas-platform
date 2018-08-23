import {RegistrationService} from '../../auth/registration.service';
import {Domain} from '../../model/domain';
import {Registration} from '../../model/registration';
import {AppConfigService} from '../../service/appconfig.service';
import {PasswordValidator} from '../../shared/common/password/password.component';
import {Component, OnInit, ViewChild} from '@angular/core';
import {FormGroup, FormBuilder, Validators} from '@angular/forms';
import {Observable} from 'rxjs/Observable';
import {NmaasModalInfoTermsComponent} from "../../shared/modal/nmaas-modal-info-terms/nmaas-modal-info-terms.component";

@Component({
  selector: 'nmaas-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.css'],
    providers: [NmaasModalInfoTermsComponent]
})
export class RegistrationComponent implements OnInit {


  public sending: boolean = false;
  public submitted: boolean = false;
  public success: boolean = false;
  public errorMessage: string = '';

    @ViewChild(NmaasModalInfoTermsComponent)
    public readonly nmaasModalInfoTerms: NmaasModalInfoTermsComponent;

  public registrationForm: FormGroup;
  public domains: Observable<Domain[]>;

  constructor(private fb: FormBuilder, private registrationService: RegistrationService, private appConfig: AppConfigService) {
    this.registrationForm = fb.group(
      {
        username: ['', [Validators.required, Validators.minLength(3)]],
        password: ['', Validators.required],
        confirmPassword: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        firstname: [''],
        lastname: [''],
        domainId: [null],
          touAccept: [false],
      },
      {
        validator: PasswordValidator.match
      });
  }

  ngOnInit() {
    this.domains = this.registrationService.getDomains()
      .map((domains) => domains.filter((domain) => domain.id !== this.appConfig.getNmaasGlobalDomainId()));
  }

  public onSubmit(): void {
    if (this.registrationForm.valid) {
      this.sending = true;
      
      const registration: Registration = new Registration(
        this.registrationForm.controls['username'].value,
        this.registrationForm.controls['password'].value,
        this.registrationForm.controls['email'].value,
        this.registrationForm.controls['firstname'].value,
        this.registrationForm.controls['lastname'].value,
        this.registrationForm.controls['domainId'].value,
          this.registrationForm.controls['touAccept'].value,
      );

      this.registrationService.register(registration).subscribe(
        (result) => {
          console.log("User registred successfully.");
          this.registrationForm.reset();
          this.sending = false;
          this.submitted = true;
          this.success = true;
        },
        (err) => {
          console.log("Unable to register user.");
          this.sending = false;
          this.submitted = true;          
          this.success = false;          
          this.errorMessage = err;
        },
        () => {
          this.sending = false;
          this.submitted = true;          
          console.log("Hmmm...");
        }
      );

    }
  }

  public refresh(): void {
    this.sending = false;
    this.submitted = false;
    this.success = false;
    this.errorMessage = '';    
  }

}

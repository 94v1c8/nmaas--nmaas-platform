import {Component, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {Router, ActivatedRoute} from '@angular/router';
import {Domain} from '../../../model/domain';
import {DomainService} from '../../../service/domain.service';
import { BaseComponent } from '../../../shared/common/basecomponent/base.component';
import {isUndefined} from 'util';
import { NG_VALIDATORS, PatternValidator } from '@angular/forms';


@Component({
  selector: 'app-domain',
  templateUrl: './domain.component.html',
  styleUrls: ['./domain.component.css'],
  providers: [{provide: NG_VALIDATORS, useExisting: PatternValidator, multi: true}]
})
export class DomainComponent extends BaseComponent implements OnInit {

  private domainId: number;
  private domain: Domain;

  constructor(protected domainService: DomainService, private router: Router, private route: ActivatedRoute, private location: Location) {
    super();
  }

  ngOnInit() {
    this.mode = this.getMode(this.route);
    this.route.params.subscribe(params => {
      if (!isUndefined(params['id'])) {
        this.domainId = +params['id'];
        this.domainService.getOne(this.domainId).subscribe((domain: Domain) => this.domain = domain);
      } else {
        this.domain = new Domain();
        this.domain.active = true;
      }
    });
  }

  protected submit(): void {
    if (!isUndefined(this.domainId)) {
      this.domainService.update(this.domain).subscribe(() => this.router.navigate(['domains/']));
    } else {
      this.domainService.add(this.domain).subscribe(() => this.router.navigate(['domains/']));
    }
  }

}

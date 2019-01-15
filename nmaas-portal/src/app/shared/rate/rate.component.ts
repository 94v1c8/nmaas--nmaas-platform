import { Component, EventEmitter, OnInit, Input, Output, ViewEncapsulation, OnChanges, SimpleChanges } from '@angular/core';

import { AppsService } from '../../service/apps.service';
import { Rate } from '../../model/rate';
import {isNullOrUndefined} from "util";

@Component({
  selector: 'rate',
  templateUrl: './rate.component.html',
  styleUrls: ['./rate.component.css', '../rating-extended/rating-extended.component.css'],
  encapsulation: ViewEncapsulation.None,
  providers: [ AppsService ]
})
export class RateComponent implements OnInit, OnChanges {

    @Input()
    private pathUrl:string;
    
    @Input()
    editable: boolean = false;
    
    rate: Rate;
    
    @Output()
    onChange = new EventEmitter<boolean>()

    @Input()
    short: boolean = false;

    @Input()
    showVotes: boolean = false;

    totalVotes: number = 0;
    
  constructor(private appsService:AppsService) { }

  ngOnInit() {
      this.refresh();
  }

  ngOnChanges(changes: SimpleChanges) {
  	if (changes['pathUrl'])
  		this.refresh();
  }
  
  public refresh(): void {
  	this.appsService.getAppRateByUrl(this.pathUrl).subscribe(rate => {
  	  this.rate = rate;
      this.countVotes();
  	});
  }
  
  public update(rate: number) {
  	if(this.editable) {
  		this.appsService.setMyAppRateByUrl(this.pathUrl + '/' + rate).subscribe( apiResponse => {
  						this.refresh();
  						this.onChange.emit(true);
  					});
  	}
  }

  public countVotes(): void{
    let oneStarCount, twoStarCount, threeStarCount, fourStarCount, fiveStarCount;
    if(!isNullOrUndefined(this.rate.rating)){
      oneStarCount = (this.rate.rating.hasOwnProperty(1) ? this.rate.rating[1] : 0);
      twoStarCount = (this.rate.rating.hasOwnProperty(2) ? this.rate.rating[2] : 0);
      threeStarCount = (this.rate.rating.hasOwnProperty(3) ? this.rate.rating[3] : 0);
      fourStarCount = (this.rate.rating.hasOwnProperty(4) ? this.rate.rating[4] : 0);
      fiveStarCount = (this.rate.rating.hasOwnProperty(5) ? this.rate.rating[5] : 0);
    }
    this.totalVotes = oneStarCount + twoStarCount + threeStarCount + fourStarCount + fiveStarCount;
  }
  
}

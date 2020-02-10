/* tslint:disable:no-unused-variable */
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {Pipe, PipeTransform} from '@angular/core';

import {AppInstanceProgressComponent} from './appinstanceprogress.component';
import {TranslateService} from "@ngx-translate/core";
import {AppInstanceProgressStage, AppInstanceState} from "../../../model";

@Pipe({
  name: "translate"
})
class TranslatePipeMock implements PipeTransform {
  public name: string = "translate";

  public transform(query: string, ...args: any[]): any {
    return query;
  }
}

class MockTranslateService{
  public instant(key: string): string{
    return "";
  }
}

describe('AppInstanceProgressComponent', () => {
  let component: AppInstanceProgressComponent;
  let fixture: ComponentFixture<AppInstanceProgressComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AppInstanceProgressComponent, TranslatePipeMock ],
      providers: [
        {provide: TranslateService, useClass: MockTranslateService},
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppInstanceProgressComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('default stage list should be length 0', () => {
    expect(component.stages.length).toEqual(0);
  });

  it('translate tag function should return', () => {
    expect(component.getTranslateTag("some text")).toEqual('');
  });

  it('should display with given stages', () => {
    let stages = new Array<AppInstanceProgressStage>();
    stages.push(new AppInstanceProgressStage("Stage1", AppInstanceState.RUNNING));
    component.stages = stages;
    expect(component.stages.length).toEqual(1);
    // TODO finish
  });
});

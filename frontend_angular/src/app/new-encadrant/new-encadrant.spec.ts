import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewEncadrant } from './new-encadrant';

describe('NewEncadrant', () => {
  let component: NewEncadrant;
  let fixture: ComponentFixture<NewEncadrant>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NewEncadrant]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NewEncadrant);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

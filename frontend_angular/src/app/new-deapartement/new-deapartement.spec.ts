import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewDeapartement } from './new-deapartement';

describe('NewDeapartement', () => {
  let component: NewDeapartement;
  let fixture: ComponentFixture<NewDeapartement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NewDeapartement]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NewDeapartement);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditEncadrant } from './edit-encadrant';

describe('EditEncadrant', () => {
  let component: EditEncadrant;
  let fixture: ComponentFixture<EditEncadrant>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EditEncadrant]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditEncadrant);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

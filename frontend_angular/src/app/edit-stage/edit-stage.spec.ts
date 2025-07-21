import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditStage } from './edit-stage';

describe('EditStage', () => {
  let component: EditStage;
  let fixture: ComponentFixture<EditStage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EditStage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditStage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

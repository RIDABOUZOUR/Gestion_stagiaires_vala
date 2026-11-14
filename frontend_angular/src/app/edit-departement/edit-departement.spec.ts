import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditDepartement } from './edit-departement';

describe('EditDepartement', () => {
  let component: EditDepartement;
  let fixture: ComponentFixture<EditDepartement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EditDepartement]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditDepartement);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

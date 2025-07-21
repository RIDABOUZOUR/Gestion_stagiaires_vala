import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewStagiaire } from './new-stagiaire';

describe('NewStagiaire', () => {
  let component: NewStagiaire;
  let fixture: ComponentFixture<NewStagiaire>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NewStagiaire]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NewStagiaire);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Encadrants } from './encadrants';

describe('Encadrants', () => {
  let component: Encadrants;
  let fixture: ComponentFixture<Encadrants>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Encadrants]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Encadrants);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

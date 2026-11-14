import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Departements } from './departements';
import { TEST_IMPORTS, TEST_PROVIDERS } from '../shared/testing/test-utils';

describe('Departements', () => {
  let component: Departements;
  let fixture: ComponentFixture<Departements>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: TEST_IMPORTS,
      declarations: [Departements],
      providers: TEST_PROVIDERS,
    }).compileComponents();

    fixture = TestBed.createComponent(Departements);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

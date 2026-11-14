import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Stages } from './stages';
import { TEST_IMPORTS, TEST_PROVIDERS } from '../shared/testing/test-utils';

describe('Stages', () => {
  let component: Stages;
  let fixture: ComponentFixture<Stages>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: TEST_IMPORTS,
      declarations: [Stages],
      providers: TEST_PROVIDERS,
    }).compileComponents();

    fixture = TestBed.createComponent(Stages);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

package org.springframework.springfaces.traveladvisor.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.math.MathContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/META-INF/config/data-access-config.xml")
public class CityRepositoryTest {

	@Autowired
	private CityRepository cityRepository;

	private PageRequest pageable = new PageRequest(0, 10);

	@Test
	public void shouldFindNotResultsByName() throws Exception {
		Page<City> page = cityRepository.findByNameLike("notfound", pageable);
		assertThat(page.getTotalElements(), is(0L));
	}

	@Test
	public void shouldFindSingleResultByName() throws Exception {
		Page<City> page = cityRepository.findByNameLike("Bath", pageable);
		assertThat(page.getTotalElements(), is(1L));
		assertThat(page.getContent().get(0).getName(), is("Bath"));
	}

	@Test
	public void shouldFindByNameIgnoringCase() throws Exception {
		Page<City> page = cityRepository.findByNameLike("bAtH", pageable);
		assertThat(page.getTotalElements(), is(1L));
		assertThat(page.getContent().get(0).getName(), is("Bath"));
	}

	@Test
	public void shouldFindMoreThanOneCity() throws Exception {
		Page<City> page = cityRepository.findByNameLike("Melbourne", pageable);
		assertThat(page.getTotalElements(), is(2L));
	}

	@Test
	public void shouldFindByNameAndCountry() throws Exception {
		City melbourneUsa = cityRepository.findByNameAndCountry("Melbourne", "USA");
		City melbourneAustralia = cityRepository.findByNameAndCountry("Melbourne", "Australia");
		assertThat(melbourneUsa.getName(), is("Melbourne"));
		assertThat(melbourneUsa.getCountry(), is("USA"));
		assertThat(melbourneAustralia.getName(), is("Melbourne"));
		assertThat(melbourneAustralia.getCountry(), is("Australia"));
	}

	@Test
	public void shouldReturnNullIfNotFoundByNameAndCountry() throws Exception {
		City city = cityRepository.findByNameAndCountry("Melbourne", "UK");
		assertThat(city, is(nullValue()));
	}

	@Test
	public void shouldFindAverage() throws Exception {
		pageable = new PageRequest(0, 10, new Sort(Direction.ASC, "name"));
		City city = cityRepository.findByNameAndCountry("Bath", "UK");
		Page<HotelSummary> hotels = cityRepository.getHotels(city, pageable);
		assertThat(hotels.getTotalElements(), is(2L));
		assertThat(hotels.getContent().get(0).getName(), is("Bath Travelodge"));
		double expected = (0 + 0 + 1 + 0 + 1 + 0 + 0 + 0 + 1 + 1 + 0 + 1 + 2 + 3) / 14.0;
		expected = new BigDecimal(expected, new MathContext(2)).doubleValue();
		assertThat(hotels.getContent().get(0).getAverageRating(), is(expected));
	}

	@Test
	public void shouldFindHotelsWithoutReview() throws Exception {
		City city = cityRepository.findByNameAndCountry("Tokyo", "Japan");
		Page<HotelSummary> hotels = cityRepository.getHotels(city, pageable);
		assertThat(hotels.getTotalElements(), is(1L));
		assertThat(hotels.getContent().get(0).getAverageRating(), is(nullValue()));
	}
}

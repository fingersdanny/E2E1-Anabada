package kr.kernel360.anabada.domain.tradeoffer.api;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import kr.kernel360.anabada.domain.tradeoffer.dto.CreateTradeOfferRequest;
import kr.kernel360.anabada.domain.tradeoffer.dto.FindAllTradeOfferRequest;
import kr.kernel360.anabada.domain.tradeoffer.dto.FindAllTradeOfferResponse;
import kr.kernel360.anabada.domain.tradeoffer.dto.FindTradeOfferResponse;
import kr.kernel360.anabada.domain.tradeoffer.service.TradeOfferService;
import kr.kernel360.anabada.global.FileHandler;
import kr.kernel360.anabada.global.error.code.TradeOfferErrorCode;
import kr.kernel360.anabada.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TradeOfferController {
	private final TradeOfferService tradeOfferService;
	private final FileHandler fileHandler;
	private final Path rootLocation = Paths.get("src/main/resources/static/images/tradeOffer");


	@GetMapping("/v1/trade-offers")
	public ResponseEntity<FindAllTradeOfferResponse> findAll(FindAllTradeOfferRequest findAllTradeOfferRequest,
		    											     @RequestParam(value="pageNo", defaultValue="1") int pageNo) {
		Pageable pageable = PageRequest.of(pageNo<1 ? 0 : pageNo-1, 10);
		FindAllTradeOfferResponse findAllTradeOfferResponse = tradeOfferService.findAll(findAllTradeOfferRequest, pageable);

		return ResponseEntity.ok(findAllTradeOfferResponse);
	}

	@PostMapping(path = "/v1/trade-offers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Long> create(@ModelAttribute CreateTradeOfferRequest createTradeOfferRequest,
		 							   @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
		if (imageFile != null && !imageFile.isEmpty()) {
			String imagePath = fileHandler.parseFileInfo(imageFile,"tradeOffer");
			createTradeOfferRequest.setImagePath(imagePath);
		}
		Long createdTradeOfferId = tradeOfferService.create(createTradeOfferRequest, createTradeOfferRequest.getTradeId());
		URI url = URI.create("api/v1/trades/trade_offers/" + createdTradeOfferId);

		return ResponseEntity.created(url).body(createdTradeOfferId);
	}

	@PutMapping("/v1/trade-offers/{tradeOfferId}/accept")
	public ResponseEntity<Void> accept(@PathVariable Long tradeOfferId) {
		tradeOfferService.accept(tradeOfferId);

		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/v1/trade-offers/{tradeOfferId}")
	public ResponseEntity<Long> remove(@PathVariable Long tradeOfferId) {
		Long removedTradeOfferId = tradeOfferService.remove(tradeOfferId);

		return ResponseEntity.ok(removedTradeOfferId);
	}

	@GetMapping("/v1/trade-offers/{tradeOfferId}")
	public ResponseEntity<FindTradeOfferResponse> find(@PathVariable Long tradeOfferId) {
		FindTradeOfferResponse findTradeOfferResponse = tradeOfferService.find(tradeOfferId);

		return ResponseEntity.ok(findTradeOfferResponse);
	}

	@GetMapping("/images/tradeOffer/{imageName}")
	public ResponseEntity<UrlResource> showImage(@PathVariable String imageName) {
		try {
			Path file = rootLocation.resolve(imageName);
			UrlResource resource = new UrlResource(file.toUri());
			return ResponseEntity.ok().body(resource);
		} catch (MalformedURLException e) {
			throw new BusinessException(TradeOfferErrorCode.NOT_FOUND_FILE_PATH);
		}
	}
}

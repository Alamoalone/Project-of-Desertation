package store.product;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;

    public List<Product> list() {
        return StreamSupport.stream(productRepository.findAll().spliterator(), false)
            .collect(Collectors.toList())
            .stream().map(ProductModel::to).toList();
    }

    public Product find(String id) {
        return productRepository.findById(id).orElse(null).to();
    }

    public void delete(String id) {
        productRepository.deleteById(id);
    }

    public Product create(Product in) {
        return productRepository.save(new ProductModel(in)).to();
    }

}
